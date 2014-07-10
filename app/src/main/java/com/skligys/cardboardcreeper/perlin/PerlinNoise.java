package com.skligys.cardboardcreeper.perlin;

import net.royawesome.jlibnoise.NoiseQuality;
import net.royawesome.jlibnoise.module.source.Perlin;

class PerlinNoise {
  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private int seed = 0;  // default
    private double frequency = -1.0;  // required
    private int lacunarity = 1;  // default
    private NoiseQuality noiseQuality = NoiseQuality.STANDARD;  // default
    private double persistence = -1.0;  // required
    private int octaveCount = 1;  // default

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

    Builder withLacunarity(int lacunarity) {
      if (lacunarity <= 0) {
        throw new IllegalArgumentException();
      }
      this.lacunarity = lacunarity;
      return this;
    }

    Builder withNoiseQuality(NoiseQuality noiseQuality) {
      if (noiseQuality == null) {
        throw new NullPointerException();
      }
      this.noiseQuality = noiseQuality;
      return this;
    }

    Builder withPersistence(double persistence) {
      if (persistence <= 0.0) {
        throw new IllegalArgumentException();
      }
      this.persistence = persistence;
      return this;
    }

    Builder withOctaveCount(int octaveCount) {
      if (octaveCount <= 0) {
        throw new IllegalArgumentException();
      }
      this.octaveCount = octaveCount;
      return this;
    }

    Perlin build() {
      if (frequency <= 0.0 || persistence <= 0.0) {
        throw new IllegalStateException();
      }
      Perlin result = new Perlin();
      result.setSeed(seed);
      result.setFrequency(frequency);
      result.setLacunarity(lacunarity);
      result.setNoiseQuality(noiseQuality);
      result.setPersistence(persistence);
      result.setOctaveCount(octaveCount);
      return result;
    }
  }
}
