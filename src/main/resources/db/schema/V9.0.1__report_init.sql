-- export log --
CREATE TABLE `export_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `type` ENUM('USER', 'MEMBER') NOT NULL COMMENT '导出类型',
    `status` ENUM('DELAY','PENDING','CANCEL','PROCESSING','PARTIAL_COMPLETE','INTERRUPTED','COMPLETE','FAILED') DEFAULT 'PENDING' COMMENT '导出状态',
    `search_condition` longtext COLLATE utf8mb4_general_ci COMMENT '查询条件',
    `filename` varchar(255) COMMENT '导出文件名称',
    `file_url` varchar(255) COMMENT '导出文件URL',
    `md5` char(32) COMMENT '导出文件MD5',
    `count` int(11) DEFAULT 0 COMMENT '导出总数',
    `platform` varchar(32) COMMENT '导出平台',
    `packed` bit(1) DEFAULT 0 COMMENT 'if packed',
    `created_time` datetime DEFAULT NOW(),
    `created_by` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导出日志';

-- import log --
CREATE TABLE `import_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `type` ENUM('USER', 'MEMBER') NOT NULL COMMENT '导入类型',
    `status` ENUM('DELAY','PENDING','CANCEL','PROCESSING','PARTIAL_COMPLETE','INTERRUPTED','COMPLETE','FAILED') DEFAULT 'PENDING' COMMENT '导入状态',
    `source_filename` varchar(255) COMMENT '导入文件名称',
    `source_file_md5` char(32) COMMENT '导入文件MD5',
    `file_size` bigint(20) COMMENT '导入文件大小',
    `file_url` varchar(255) COMMENT '导入文件URL',
    `err_file_url` varchar(255) COMMENT '错误结果文件',
    `count` int(10) DEFAULT 0 COMMENT '成功导入数据总量',
    `platform` varchar(32) COMMENT '导入平台',
    `created_time` datetime DEFAULT NOW(),
    `created_by` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导入日志';