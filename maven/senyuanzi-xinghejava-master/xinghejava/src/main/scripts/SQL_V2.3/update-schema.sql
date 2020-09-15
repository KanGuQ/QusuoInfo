--- v0.0.0.1版本
/**tbAccount  住户sql **/
alter table tbAccount add zoom int(5) ;
alter table tbAccount add lat double(12,8) ;
alter table tbAccount add lng double(12,8) ;
alter table tbAccount add adCode varchar(255) ;
alter table tbAccount add countyId varchar(255) ;
alter table tbAccount add provinceId varchar(255) ;
-- ----------------------------
-- Table structure for tbarea
-- ----------------------------
/**tbCommounty  小区表sql**/
CREATE TABLE `tbarea`  (
  `id` int(10) NOT NULL COMMENT '主键id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `parent_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '父类id',
  `short_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '简称',
  `level_type` int(2) NULL DEFAULT NULL COMMENT '等级',
  `zip_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邮政编码',
  `city_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '城市编码',
  `tree_names` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '父类名称集合',
  `lng` decimal(12, 7) NULL DEFAULT NULL COMMENT '经度',
  `lat` double(12, 7) NULL DEFAULT NULL COMMENT '纬度',
  `pin_yin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '对应的城市汉语拼音',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '区域表' ROW_FORMAT = Dynamic;
/**小区对谁可见表**/
CREATE TABLE `tbcommunityvisibleuser`  (
  `AccountId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `communityId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `SortNumber` int(11) NOT NULL,
  `UserId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `UserName` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`AccountId`, `communityId`, `SortNumber`, `UserId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '小区对用户可见表' ROW_FORMAT = Dynamic;

/** 小区信息**/
CREATE TABLE `tbcommunity`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '主键',
  `accountId` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '租户id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '小区名称',
  `provinceId` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '所属省份',
  `provinceName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '省份名称',
  `cityId` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '所属的城市',
  `cityName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '城市名称',
  `countyName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '区域名称',
  `countyId` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '所属的区县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '小区的详细地址',
  `lat` double(12, 8) NULL DEFAULT NULL,
  `values` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '小区的经纬度 坐标点集合',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '小区的备注信息',
  `filePath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件存储路径',
  `pictureUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '图片的访问路径',
  `createUserId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建用户id',
  `createUserName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建用户的用户名',
  `createDate` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `lastUpdateDate` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '最后更新时间',
  `lng` double(12, 8) NULL DEFAULT NULL,
  `chargerId` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '负责人id',
  PRIMARY KEY (`id`) USING BTREE
)
--- v0.0.0.2版本
/**小区与项目关系表**/
CREATE TABLE `tbCommunityProjectRelated`  (
  `AccountId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `communityId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `SortNumber` int(11) NOT NULL,
  `ProjectId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ProjectName` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '项目名称',
  `Status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '项目状态  α 机会  β 在建  γ 售后',
  PRIMARY KEY (`AccountId`, `communityId`, `SortNumber`, `ProjectId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '小区与项目关联表' ROW_FORMAT = Dynamic;

/**小区与干系人表**/
CREATE TABLE `tbCommunityPeopleRelated`  (
  `AccountId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `communityId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `SortNumber` int(11) NOT NULL,
  `PeopleId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '干系人id',
  `PeopleName` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '干系人名称',
  `RelatedPeopleRole` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关系',
  PRIMARY KEY (`AccountId`, `communityId`, `SortNumber`, `PeopleId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '小区与干系人关系表' ROW_FORMAT = Dynamic;
