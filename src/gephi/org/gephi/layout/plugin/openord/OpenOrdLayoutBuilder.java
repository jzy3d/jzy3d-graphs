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

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = LayoutBuilder.class)
public class OpenOrdLayoutBuilder implements LayoutBuilder {

    private OpenOrdLayoutUI ui = new OpenOrdLayoutUI();

    @Override
    public String getName() {
        return "OpenOrd";
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    @Override
    public Layout buildLayout() {
        return new OpenOrdLayout(this);
    }

    private static class OpenOrdLayoutUI implements LayoutUI {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(OpenOrdLayoutBuilder.class, "OpenOrd.description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 3;
        }

        @Override
        public int getSpeedRank() {
            return 5;
        }
    }
}
