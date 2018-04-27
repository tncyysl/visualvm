/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.tools.visualvm.heapviewer.truffle.lang.ruby;

import com.sun.tools.visualvm.heapviewer.HeapContext;
import com.sun.tools.visualvm.heapviewer.model.HeapViewerNode;
import com.sun.tools.visualvm.heapviewer.truffle.TruffleObjectPreviewPlugin;
import com.sun.tools.visualvm.heapviewer.truffle.TruffleObjectPropertyPlugin;
import com.sun.tools.visualvm.heapviewer.ui.HeapViewPlugin;
import com.sun.tools.visualvm.heapviewer.ui.HeapViewerActions;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
final class RubyViewPlugins {
    
    // -------------------------------------------------------------------------
    // --- Preview -------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    static class PreviewPlugin extends TruffleObjectPreviewPlugin {
    
        PreviewPlugin(HeapContext context) {
            super(context);
        }


        @Override
        protected boolean supportsNode(HeapViewerNode node) {
            return node instanceof RubyNodes.RubyObjectNode;
        }

        @Override
        protected Instance getPreviewInstance(HeapViewerNode node) {
            RubyNodes.RubyObjectNode dnode = (RubyNodes.RubyObjectNode)node;
            if ("Proc".equals(dnode.getTypeName())) {
                RubyObject rbobj = dnode.getTruffleObject();
                FieldValue dataField = rbobj.getFieldValue("sharedMethodInfo (hidden)");
                Instance data = dataField instanceof ObjectFieldValue ? ((ObjectFieldValue)dataField).getInstance() : null;
                if (data == null) return null;

                Object sourceSection = ((Instance)data).getValueOfField("sourceSection");
                if (!(sourceSection instanceof Instance)) return null;

                return (Instance)sourceSection;
            } else {
                return null;
            }
        }
    
    }
    
    
    @ServiceProvider(service=HeapViewPlugin.Provider.class, position = 100)
    public static class PreviewPluginProvider extends HeapViewPlugin.Provider {

        public HeapViewPlugin createPlugin(HeapContext context, HeapViewerActions actions, String viewID) {
            if (RubyHeapFragment.isRubyHeap(context))
                return new PreviewPlugin(context);
            return null;
        }
        
    }
    
    
    // -------------------------------------------------------------------------
    // --- Fields --------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    @ServiceProvider(service=HeapViewPlugin.Provider.class, position = 200)
    public static class FieldsPluginProvider extends HeapViewPlugin.Provider {

        public HeapViewPlugin createPlugin(HeapContext context, HeapViewerActions actions, String viewID) {
            if (!RubyHeapFragment.isRubyHeap(context)) return null;
            
            RubyObjectProperties.FieldsProvider fieldsProvider = Lookup.getDefault().lookup(RubyObjectProperties.FieldsProvider.class);
            return new TruffleObjectPropertyPlugin("Variables", "Variables", Icons.getIcon(ProfilerIcons.NODE_FORWARD), "ruby_objects_fields", context, actions, fieldsProvider);
        }
        
    }
    
    
    // -------------------------------------------------------------------------
    // --- References ----------------------------------------------------------
    // -------------------------------------------------------------------------
    
    @ServiceProvider(service=HeapViewPlugin.Provider.class, position = 400)
    public static class ReferencesPluginProvider extends HeapViewPlugin.Provider {

        public HeapViewPlugin createPlugin(HeapContext context, HeapViewerActions actions, String viewID) {
            if (!RubyHeapFragment.isRubyHeap(context)) return null;
            
            RubyObjectProperties.ReferencesProvider fieldsProvider = Lookup.getDefault().lookup(RubyObjectProperties.ReferencesProvider.class);
            return new TruffleObjectPropertyPlugin("References", "References", Icons.getIcon(ProfilerIcons.NODE_REVERSE), "ruby_objects_references", context, actions, fieldsProvider);
        }
        
    }
    
}