package com.gtt.components;

import com.gtt.views.DefaultView;

import javafx.scene.layout.AnchorPane;

public abstract class DefaultComponent extends AnchorPane {

    private DefaultView view = null;

    public DefaultView getRoot() {
        return view;
    }

    public void setRoot(DefaultView view) {
        this.view = view;
    }

}
