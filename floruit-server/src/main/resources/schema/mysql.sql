USE
`floruit`;
SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `t_floruit_segment`;
CREATE TABLE `t_floruit_segment`
(
    `key`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '业务主键',
    `max_id`      bigint UNSIGNED NOT NULL COMMENT '号段最大ID',
    `step`        bigint UNSIGNED NOT NULL COMMENT '号段步长',
    `create_time` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;
SET
FOREIGN_KEY_CHECKS = 1;