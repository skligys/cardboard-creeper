package com.skligys.cardboardcreeper.perlin;

import net.royawesome.jlibnoise.module.Module;

class Turbulence {
  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private Module source = null;  // required
    private int seed = 0;  // default
    private double frequency = -1.0;  // required
    private double power = -1.0;  // required
    private int roughness = -1;  // required

    Builder withSource(Module source) {
      if (source == null) {
        throw new NullPointerException();
      }
      this.source = source;
      return this;
    }

    Builder withSeed(int seed) {
      this.seed = seed;
      return this;
    }

    Builder withFrequency(double frequency) {
      if (frequency <= 0.0) {
        throw new IllegalArgumentException();
      }
      this.frequency = frequency;
      return this;
    }

    Builder withPower(double power) {
      if (power <= 0.0) {
        throw new IllegalArgumentException();
      }
      this.power = power;
      return this;
    }

    Builder withRoughness(int roughness) {
      if (roughness <= 0) {
        throw new IllegalArgumentException();
      }
      this.roughness = roughness;
      return this;
    }

    net.royawesome.jlibnoise.module.modifier.Turbulence build() {
      if (source == null) {
        throw new NullPointerException();
      }
      if (frequency <= 0.0 || power <= 0.0 || roughness <= 0) {
        throw new IllegalStateException();
      }
      net.royawesome.jlibnoise.module.modifier.Turbulence result =
          new net.royawesome.jlibnoise.module.modifier.Turbulence();
      result.SetSourceModule(0, source);
      result.setSeed(seed);
      result.setFrequency(frequency);
      result.setPower(power);
      result.setRoughness(roughness);
      return result;
    }
  }
}
