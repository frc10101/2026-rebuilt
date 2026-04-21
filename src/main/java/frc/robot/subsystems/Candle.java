package frc.robot.subsystems;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CANdleConfiguration;
import com.ctre.phoenix6.controls.ColorFlowAnimation;
import com.ctre.phoenix6.controls.EmptyAnimation;
import com.ctre.phoenix6.controls.FireAnimation;
import com.ctre.phoenix6.controls.LarsonAnimation;
import com.ctre.phoenix6.controls.RainbowAnimation;
import com.ctre.phoenix6.controls.RgbFadeAnimation;
import com.ctre.phoenix6.controls.SingleFadeAnimation;
import com.ctre.phoenix6.controls.SolidColor;
import com.ctre.phoenix6.controls.StrobeAnimation;
import com.ctre.phoenix6.controls.TwinkleAnimation;
import com.ctre.phoenix6.controls.TwinkleOffAnimation;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.AnimationDirectionValue;
import com.ctre.phoenix6.signals.RGBWColor;
import com.ctre.phoenix6.signals.StatusLedWhenActiveValue;
import com.ctre.phoenix6.signals.StripTypeValue;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LEDConstants;
import org.littletonrobotics.junction.Logger;

/** LED subsystem backed by CTRE CANdle. */
public class Candle extends SubsystemBase {
  private static final RGBWColor BLACK = new RGBWColor(1, 1, 1, 0); // #010101
  private static final RGBWColor WHITE = new RGBWColor(255, 255, 255, 0); // #ffffff
  private static final RGBWColor CYAN = new RGBWColor(0, 193, 193, 0); // #00c1c1
  private static final RGBWColor RED = new RGBWColor(241, 1, 1, 0); // #f10101
  private static final RGBWColor Yellow = new RGBWColor(251, 202, 19, 0); // #fbca13

  public enum AnimationType {
    NONE,
    COLOR_FLOW,
    FIRE,
    LARSON,
    RAINBOW,
    RGB_FADE,
    SINGLE_FADE,
    STROBE,
    TWINKLE,
    TWINKLE_OFF
  }

  private final CANdle candle = new CANdle(LEDConstants.CANDLE_CAN_ID, CANBus.roboRIO());

  private AnimationType slot0State = AnimationType.NONE;
  private AnimationType slot1State = AnimationType.NONE;

  public Candle() {
    CANdleConfiguration cfg = new CANdleConfiguration();
    cfg.LED.StripType = StripTypeValue.GRB;
    cfg.LED.BrightnessScalar = LEDConstants.BRIGHTNESS_SCALAR;
    cfg.CANdleFeatures.StatusLedWhenActive = StatusLedWhenActiveValue.Disabled;
    candle.getConfigurator().apply(cfg);

    clearAllAnimations();
    candle.setControl(new SolidColor(0, 3).withColor(BLACK));
    candle.setControl(new SolidColor(4, 7).withColor(WHITE));

    setSlot0Animation(AnimationType.COLOR_FLOW);
    setSlot1Animation(AnimationType.LARSON);
  }

  public void clearAllAnimations() {
    for (int slot = 0; slot < 8; slot++) {
      candle.setControl(new EmptyAnimation(slot));
    }
    slot0State = AnimationType.NONE;
    slot1State = AnimationType.NONE;
  }

  public void setSlot0Animation(AnimationType animation) {
    if (animation == null || animation == slot0State) {
      return;
    }

    slot0State = animation;
    switch (animation) {
      default:
      case NONE:
        candle.setControl(new EmptyAnimation(0));
        break;
      case COLOR_FLOW:
        candle.setControl(
            new ColorFlowAnimation(LEDConstants.SLOT0_START_IDX, LEDConstants.SLOT0_END_IDX)
                .withSlot(0)
                .withColor(CYAN));
        break;
      case RAINBOW:
        candle.setControl(
            new RainbowAnimation(LEDConstants.SLOT0_START_IDX, LEDConstants.SLOT0_END_IDX)
                .withSlot(0));
        break;
      case TWINKLE:
        candle.setControl(
            new TwinkleAnimation(LEDConstants.SLOT0_START_IDX, LEDConstants.SLOT0_END_IDX)
                .withSlot(0)
                .withColor(CYAN));
        break;
      case TWINKLE_OFF:
        candle.setControl(
            new TwinkleOffAnimation(LEDConstants.SLOT0_START_IDX, LEDConstants.SLOT0_END_IDX)
                .withSlot(0)
                .withColor(CYAN));
        break;
      case FIRE:
        candle.setControl(
            new FireAnimation(LEDConstants.SLOT0_START_IDX, LEDConstants.SLOT0_END_IDX)
                .withSlot(0));
        break;
    }
  }

  public void setSlot1Animation(AnimationType animation) {
    if (animation == null || animation == slot1State) {
      return;
    }

    slot1State = animation;
    switch (animation) {
      default:
      case NONE:
        candle.setControl(new EmptyAnimation(1));
        break;
      case LARSON:
        candle.setControl(
            new LarsonAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1)
                .withColor(RED));
        break;
      case RGB_FADE:
        candle.setControl(
            new RgbFadeAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1));
        break;
      case SINGLE_FADE:
        candle.setControl(
            new SingleFadeAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1)
                .withColor(Yellow));
        break;
      case STROBE:
        candle.setControl(
            new StrobeAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1)
                .withColor(Yellow));
        break;
      case FIRE:
        candle.setControl(
            new FireAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1)
                .withDirection(AnimationDirectionValue.Backward)
                .withCooling(0.4)
                .withSparking(0.5));
        break;
      case COLOR_FLOW:
        candle.setControl(
            new ColorFlowAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1)
                .withColor(Yellow));
        break;
      case RAINBOW:
        candle.setControl(
            new RainbowAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1));
        break;
      case TWINKLE:
        candle.setControl(
            new TwinkleAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1)
                .withColor(Yellow));
        break;
      case TWINKLE_OFF:
        candle.setControl(
            new TwinkleOffAnimation(LEDConstants.SLOT1_START_IDX, LEDConstants.SLOT1_END_IDX)
                .withSlot(1)
                .withColor(Yellow));
        break;
    }
  }

  public AnimationType getSlot0State() {
    return slot0State;
  }

  public AnimationType getSlot1State() {
    return slot1State;
  }

  @Override
  public void periodic() {
    Logger.recordOutput("LED/Slot0", slot0State.name());
    Logger.recordOutput("LED/Slot1", slot1State.name());
  }
}
