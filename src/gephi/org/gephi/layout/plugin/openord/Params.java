/*
Copyright 2007 Sandia Corporation. Under the terms of Contract
DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government retains
certain rights in this software.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
 * Neither the name of Sandia National Laboratories nor the names of
its contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gephi.layout.plugin.openord;

/**
 *
 * @author Mathieu Bastian
 */
public enum Params {

    DEFAULT(new Stage(0, 2000f, 10f, 1f),
    new Stage(0.25f, 2000f, 10f, 1f),
    new Stage(0.25f, 2000f, 2f, 1f),
    new Stage(0.25f, 2000f, 1f, 0.1f),
    new Stage(0.10f, 250f, 1f, 0.25f),
    new Stage(0.15f, 250f, 0.5f, 0f)),
    COARSEN(new Stage(0, 2000f, 10f, 1f),
    new Stage(200, 2000f, 2f, 1f),
    new Stage(200, 2000f, 10f, 1f),
    new Stage(200, 2000f, 1f, 0.1f),
    new Stage(50, 250f, 1f, 0.25f),
    new Stage(100, 250f, 0.5f, 0f)),
    COARSEST(new Stage(0, 2000f, 10f, 1f),
    new Stage(200, 2000f, 2f, 1f),
    new Stage(200, 2000f, 10f, 1f),
    new Stage(200, 2000f, 1f, 0.1f),
    new Stage(200, 250f, 1f, 0.25f),
    new Stage(100, 250f, 0.5f, 0f)),
    REFINE(new Stage(0, 50f, 0.5f, 0f),
    new Stage(0, 2000f, 2f, 1f),
    new Stage(50, 500f, 0.1f, 0.25f),
    new Stage(50, 200f, 1f, 0.1f),
    new Stage(50, 250f, 1f, 0.25f),
    new Stage(0, 250f, 0.5f, 0f)),
    FINAL(new Stage(0, 50f, 0.5f, 0f),
    new Stage(0, 2000f, 2f, 1f),
    new Stage(50, 50f, 0.1f, 0.25f),
    new Stage(50, 200f, 1f, 0.1f),
    new Stage(50, 250f, 1f, 0.25f),
    new Stage(25, 250f, 0.5f, 0f));
    private final Stage initial;
    private final Stage liquid;
    private final Stage expansion;
    private final Stage cooldown;
    private final Stage crunch;
    private final Stage simmer;

    private Params(Stage initial, Stage liquid, Stage expansion, Stage cooldown, Stage crunch, Stage simmer) {
        this.initial = initial;
        this.liquid = liquid;
        this.expansion = expansion;
        this.cooldown = cooldown;
        this.crunch = crunch;
        this.simmer = simmer;
    }

    public Stage getCooldown() {
        return cooldown;
    }

    public Stage getCrunch() {
        return crunch;
    }

    public Stage getExpansion() {
        return expansion;
    }

    public Stage getInitial() {
        return initial;
    }

    public Stage getLiquid() {
        return liquid;
    }

    public Stage getSimmer() {
        return simmer;
    }

    public float getIterationsSum() {
        return liquid.iterations + expansion.iterations + cooldown.iterations + crunch.iterations + simmer.iterations;
    }

    public static class Stage {

        private float iterations;
        private float temperature;
        private float attraction;
        private float dampingMult;

        Stage(float iterations, float temperature, float attraction, float dampingMult) {
            this.iterations = iterations;
            this.temperature = temperature;
            this.attraction = attraction;
            this.dampingMult = dampingMult;
        }

        public float getAttraction() {
            return attraction;
        }

        public float getDampingMult() {
            return dampingMult;
        }

        public float getIterations() {
            return iterations;
        }

        public int getIterationsTotal(int totalIterations) {
            return (int) (iterations * totalIterations);
        }

        public int getIterationsPercentage() {
            return (int) (iterations * 100f);
        }

        public float getTemperature() {
            return temperature;
        }

        public void setIterations(float iterations) {
            this.iterations = iterations;
        }
    }
}
