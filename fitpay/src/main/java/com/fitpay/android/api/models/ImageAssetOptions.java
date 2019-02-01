package com.fitpay.android.api.models;

import java.util.HashMap;
import java.util.Map;

public class ImageAssetOptions {

    public enum ImageAssetParams {
        WIDTH("w"),
        HEIGHT("h"),
        EMBOSSED_TEXT("embossedText"),
        EMBOSSED_TEXT_FOREGROUND("embossedForegroundColor"),
        FONT_SCALE("fs"),
        TEXT_POSITION_X_SCALE("txs"),
        TEXT_POSITION_Y_SCALE("tys"),
        FONT_NAME("fn"),
        FONT_BOLD("fb"),
        ROUNDED_CORNERS("rc");

        public String value;

        ImageAssetParams(String param) {
            this.value = param;
        }
    }

    private Integer width;
    private Integer height;
    private String embossedText;
    private String foregroundColor;
    private Integer fontScale;
    private Float textPositionXScale;
    private Float textPositionYScale;
    private String fontName;
    private Boolean fontBold;
    private Boolean roundedCorners;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getEmbossedText() {
        return embossedText;
    }

    public ImageAssetOptions setEmbossedText(String embossedText) {
        this.embossedText = embossedText;
        return this;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    public ImageAssetOptions setForegroundColor(String foregroundColor) {
        this.foregroundColor = foregroundColor;
        return this;
    }

    public Integer getFontScale() {
        return fontScale;
    }

    public void setFontScale(Integer fontScale) {
        this.fontScale = fontScale;
    }

    public Float getTextPositionXScale() {
        return textPositionXScale;
    }

    public void setTextPositionXScale(Float textPositionXScale) {
        this.textPositionXScale = textPositionXScale;
    }

    public Float getTextPositionYScale() {
        return textPositionYScale;
    }

    public void setTextPositionYScale(Float textPositionYScale) {
        this.textPositionYScale = textPositionYScale;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public Boolean isFontBold() {
        return fontBold;
    }

    public void setFontBold(Boolean fontBold) {
        this.fontBold = fontBold;
    }

    public Boolean isRoundedCorners() {
        return roundedCorners;
    }

    public void setRoundedCorners(Boolean roundedCorners) {
        this.roundedCorners = roundedCorners;
    }

    public static class Builder {

        public Builder() {}

        private int width;
        private int height;
        private String embossedText;
        private String foregroundColor;
        private int fontScale;
        private float textPositionXScale;
        private float textPositionYScale;
        private String fontName;
        private boolean fontBold;
        private boolean roundedCorners;

        public ImageAssetOptions build() {
            ImageAssetOptions options = new ImageAssetOptions();
            options.height = this.height;
            options.width = this.width;
            options.embossedText = this.embossedText;
            options.foregroundColor = this.foregroundColor;
            options.fontScale = this.fontScale;
            options.textPositionXScale = this.textPositionXScale;
            options.textPositionYScale = this.textPositionYScale;
            options.fontName = this.fontName;
            options.fontBold = this.fontBold;
            options.roundedCorners = this.roundedCorners;
            return options;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setEmbossedText(String embossedText) {
            this.embossedText = embossedText;
            return this;
        }

        public Builder setForegroundColor(String foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        public Builder setFontScale(int fontScale) {
            this.fontScale = fontScale;
            return this;
        }

        public Builder setTextPositionXScale(float textPositionXScale) {
            this.textPositionXScale = textPositionXScale;
            return this;
        }

        public Builder setTextPositionYScale(float textPositionYScale) {
            this.textPositionYScale = textPositionYScale;
            return this;
        }

        public Builder setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public Builder setFontBold(boolean fontBold) {
            this.fontBold = fontBold;
            return this;
        }

        public Builder setRoundedCorners(boolean roundedCorners) {
            this.roundedCorners = roundedCorners;
            return this;
        }
    }

    public Map<String, String> getParamToValueMap() {
        Map<String, String> paramToValueMap = new HashMap<>();
        if (null != this.width)
            paramToValueMap.put(ImageAssetParams.WIDTH.value, String.valueOf(this.width));
        if (null != this.height)
            paramToValueMap.put(ImageAssetParams.HEIGHT.value, String.valueOf(this.height));
        if (null != this.embossedText)
            paramToValueMap.put(ImageAssetParams.EMBOSSED_TEXT.value, this.embossedText);
        if (null != this.foregroundColor)
            paramToValueMap.put(ImageAssetParams.EMBOSSED_TEXT_FOREGROUND.value, this.foregroundColor);
        if (null != this.fontScale)
            paramToValueMap.put(ImageAssetParams.FONT_SCALE.value, String.valueOf(this.fontScale));
        if (null != this.textPositionXScale)
            paramToValueMap.put(ImageAssetParams.TEXT_POSITION_X_SCALE.value, String.valueOf(this.textPositionXScale));
        if (null != this.textPositionYScale)
            paramToValueMap.put(ImageAssetParams.TEXT_POSITION_Y_SCALE.value, String.valueOf(this.textPositionYScale));
        if (null != this.fontName)
            paramToValueMap.put(ImageAssetParams.FONT_NAME.value, String.valueOf(this.fontName));
        if (null != this.fontBold)
            paramToValueMap.put(ImageAssetParams.FONT_BOLD.value, String.valueOf(this.fontBold));
        if (null != this.roundedCorners)
            paramToValueMap.put(ImageAssetParams.ROUNDED_CORNERS.value, String.valueOf(this.roundedCorners));
            
        return paramToValueMap;
    }

}
