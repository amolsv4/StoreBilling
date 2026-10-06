package com.retailpos.terminal.entity;

/** Terminal device type. Stored as VARCHAR(20) via {@code @Enumerated(EnumType.STRING)}. */
public enum TerminalType {
    POS,
    MPOS,
    KIOSK
}
