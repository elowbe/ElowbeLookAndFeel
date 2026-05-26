package com.elowbe.laf.node;

import javax.swing.JPopupMenu;

@FunctionalInterface
public interface NodeContextMenuContributor {
    void contribute(NodeCanvas canvas, NodeComponent node, JPopupMenu menu);
}
