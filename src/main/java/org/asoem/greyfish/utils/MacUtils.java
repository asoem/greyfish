package org.asoem.greyfish.utils;

import javax.swing.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 11.04.11
 * Time: 15:25
 */
public class MacUtils {

    public enum ButtonStyle {
        DEFAULT("default"),
        ROUND_RECT("roundRect"),
        RECESSED("recessed"),
        BEVEL("bevel"),
        TEXTURED("textured"),
        SQUARE("square"),
        GRADIENT("gradient"),
        HELP("help");

        String propertyName;

        ButtonStyle(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }

    public enum SliderShape {
        ARROW,
        ROUND
    }

    public enum SegmentedButtonStyle {
        SEGMENTED("segmented"),
        SEGMENTED_ROUND_RECT("segmentedRoundRect"),
        SEGMENTED_CAPSULE("segmentedCapsule"),
        SEGMENTED_TEXTURED("segmentedTextured"),
        SEGMENTED_GRADIENT("segmentedGradient");

        String propertyName;

        SegmentedButtonStyle(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }

    public enum ComponentSize {
        REGULAR("regular"),
        SMALL("small"),
        MINI("mini");

        String propertyName;

        ComponentSize(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }

    public static JButton newJButton(ButtonStyle buttonStyle, ComponentSize componentSize) {
        JButton ret = new JButton();
        changeButtonStyle(ret, buttonStyle, componentSize);
        return ret;
    }

    public static void changeButtonStyle(AbstractButton button, ButtonStyle buttonStyle, ComponentSize componentSize) {
        button.putClientProperty("JComponent.sizeVariant", componentSize.getPropertyName());
        button.putClientProperty("JButton.buttonType", buttonStyle.getPropertyName());
    }

    public static void changeSliderStyle(JSlider button, SliderShape buttonShape, ComponentSize componentSize) {
        button.putClientProperty("JComponent.sizeVariant", componentSize.getPropertyName());
        button.putClientProperty("Slider.paintThumbArrowShape", buttonShape == MacUtils.SliderShape.ARROW);
    }

    public static Configurer makeButtonsSegmented(SegmentedButtonStyle buttonStyle, ComponentSize componentSize) {
        return new Configurer(buttonStyle, componentSize);
    }

    public static class Configurer {

        private final SegmentedButtonStyle buttonStyle;
        private final ComponentSize componentSize;

        public Configurer(SegmentedButtonStyle buttonStyle, ComponentSize componentSize) {
            checkNotNull(buttonStyle);
            checkNotNull(componentSize);
            this.buttonStyle = buttonStyle;
            this.componentSize = componentSize;
        }

        private void setCommonProperties(AbstractButton button) {
            button.putClientProperty("JComponent.sizeVariant", componentSize.getPropertyName());
            button.putClientProperty("JButton.buttonType", buttonStyle.getPropertyName() );
        }

        public Configurer first(AbstractButton button) {
            setCommonProperties(button);
            button.putClientProperty("JButton.segmentPosition", "first");
            return this;
        }

        public Configurer middle(AbstractButton ... buttons) {
            for (AbstractButton button : buttons) {
                setCommonProperties(button);
                button.putClientProperty("JButton.segmentPosition", "middle" );
            }
            return this;
        }

        public Configurer last(AbstractButton button) {
            setCommonProperties(button);
            button.putClientProperty("JButton.segmentPosition", "last" );
            return this;
        }
    }
}
