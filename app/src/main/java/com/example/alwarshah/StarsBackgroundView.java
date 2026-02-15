package com.example.alwarshah;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom View that displays twinkling stars based on the golden ratio.
 * Stars are positioned using the golden angle (137.5°) for optimal distribution.
 */
public class StarsBackgroundView extends View {
    
    // Golden ratio constant (φ)
    private static final double PHI = 1.618033988749895;
    
    // Golden angle in degrees (360 / φ²) ≈ 137.5°
    private static final double GOLDEN_ANGLE = 137.508;
    
    private List<Star> stars;
    private Paint starPaint;
    private Random random;
    private ValueAnimator animator;
    
    public StarsBackgroundView(Context context) {
        super(context);
        init();
    }
    
    public StarsBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StarsBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        stars = new ArrayList<>();
        random = new Random();
        
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setStyle(Paint.Style.FILL);
        
        // Set dark blue background color
        setBackgroundColor(0xFF0A1628);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateStars(w, h);
        startTwinkleAnimation();
    }
    
    /**
     * Generate stars using golden ratio for positioning.
     * Uses Fibonacci spiral pattern based on golden angle.
     */
    private void generateStars(int width, int height) {
        stars.clear();
        
        // Number of stars based on screen area and golden ratio
        int numStars = (int) (Math.sqrt(width * height) * PHI);
        
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        
        // Maximum radius for star distribution
        double maxRadius = Math.sqrt(centerX * centerX + centerY * centerY);
        
        for (int i = 0; i < numStars; i++) {
            // Golden angle spiral positioning
            double angle = i * GOLDEN_ANGLE;
            double angleRad = Math.toRadians(angle);
            
            // Radius grows with square root for even distribution
            double radius = maxRadius * Math.sqrt((double) i / numStars);
            
            // Calculate position using polar coordinates
            float x = (float) (centerX + radius * Math.cos(angleRad));
            float y = (float) (centerY + radius * Math.sin(angleRad));
            
            // Ensure stars are within bounds
            if (x >= 0 && x <= width && y >= 0 && y <= height) {
                // Star size based on golden ratio
                float baseSize = (float) (2.0 + (i % (int)PHI) * PHI);
                
                // Brightness varies for depth effect
                float brightness = 0.5f + random.nextFloat() * 0.5f;
                
                // Random twinkle phase for varied animation
                float phase = random.nextFloat() * (float) (2 * Math.PI);
                
                // Twinkle speed varies inversely with size (smaller stars twinkle faster)
                float speed = (float) (PHI / baseSize);
                
                stars.add(new Star(x, y, baseSize, brightness, phase, speed));
            }
        }
    }
    
    /**
     * Start the continuous twinkling animation.
     */
    private void startTwinkleAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        
        animator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        animator.setDuration(5000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float time = (Float) animation.getAnimatedValue();
                updateStars(time);
                invalidate();
            }
        });
        animator.start();
    }
    
    /**
     * Update star brightness based on animation time.
     */
    private void updateStars(float time) {
        for (Star star : stars) {
            // Calculate twinkle intensity using sine wave
            float twinkle = (float) Math.sin(time * star.speed + star.phase);
            star.currentBrightness = star.baseBrightness * (0.3f + 0.7f * (twinkle + 1) / 2);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (Star star : stars) {
            drawStar(canvas, star);
        }
    }
    
    /**
     * Draw a single star with glow effect.
     */
    private void drawStar(Canvas canvas, Star star) {
        // Calculate color with current brightness
        int alpha = (int) (255 * star.currentBrightness);
        int color = (alpha << 24) | 0x00FFFFFF; // White with variable alpha
        
        // Create radial gradient for glow effect
        RadialGradient gradient = new RadialGradient(
            star.x, star.y, star.size * (float) PHI,
            new int[]{color, 0x00FFFFFF},
            new float[]{0f, 1f},
            Shader.TileMode.CLAMP
        );
        
        starPaint.setShader(gradient);
        
        // Draw the star with glow
        canvas.drawCircle(star.x, star.y, star.size * (float) PHI, starPaint);
        
        // Draw bright center
        starPaint.setShader(null);
        starPaint.setColor(color);
        canvas.drawCircle(star.x, star.y, star.size, starPaint);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }
    
    /**
     * Inner class representing a single star.
     */
    private static class Star {
        float x, y;                  // Position
        float size;                  // Base size
        float baseBrightness;        // Base brightness (0-1)
        float currentBrightness;     // Current animated brightness
        float phase;                 // Animation phase offset
        float speed;                 // Twinkle speed multiplier
        
        Star(float x, float y, float size, float brightness, float phase, float speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.baseBrightness = brightness;
            this.currentBrightness = brightness;
            this.phase = phase;
            this.speed = speed;
        }
    }
}
