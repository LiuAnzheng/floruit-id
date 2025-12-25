package org.laz.floruitid.floruitclient.common.enums;

import lombok.Getter;

/**
 * 支持的Id模式
 */
@Getter
public enum IdMode {
    SNOW_FLAKE("snowflake"),
    SEGMENT("segment");

    private final String mode;

    IdMode(String mode) {
        this.mode = mode;
    }
}
