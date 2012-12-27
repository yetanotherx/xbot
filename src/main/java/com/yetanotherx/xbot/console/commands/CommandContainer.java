package com.yetanotherx.xbot.console.commands;

import com.yetanotherx.xbot.XBot;

/**
 * Simply stores the controller in a protected
 * field. It also provides an interface to ensure
 * that only command classes get registered.
 * 
 */
public abstract class CommandContainer {

    protected XBot parent;

    public CommandContainer(XBot parent) {
        this.parent = parent;
    }
}
