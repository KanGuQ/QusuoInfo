alter table `tbpeople`
    ADD COLUMN `DepartmentId` int(11) AFTER `PeopleId`;

alter table `tbproject`
    ADD COLUMN `DepartmentId` int(11) AFTER `ProjectId`;

alter table `tbuser`
    ADD COLUMN `DepartmentId` int(11) AFTER `UserId`,
    ADD COLUMN `CurrentDepartmentId` int(11) NULL DEFAULT NULL AFTER `LastAccessed`;

alter table `tbquotationbill`
    ADD COLUMN `DepartmentId` int(11) AFTER `BillId`;

alter table `tbrecord`
    ADD COLUMN `DepartmentId` int(11) AFTER `RecordId`;

alter table `staff_join_record`
    ADD COLUMN `departmentId` int(11) AFTER `userId`;

alter table `tbproject`
    ADD COLUMN `HandoverUserId` varchar(50) AFTER `CreateDateTime`,
    ADD COLUMN `HandoverUserName` varchar(50) AFTER `HandoverUserId`;

alter table `tbpeople`
    ADD COLUMN `HandoverUserId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL AFTER `CreateDateTime`,
    ADD COLUMN `HandoverUserName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL AFTER `HandoverUserId`;

-- 企业数据表
create TABLE `WholesalerData` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `companyId` varchar(255) DEFAULT NULL,
  `productDownloadCount` bigint(20) NOT NULL DEFAULT 0,
  `productQuotationCount` bigint(20) NOT NULL DEFAULT 0,
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleteTime` datetime DEFAULT NULL,
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_companyId` (`companyId`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

-- 产品数据表
create TABLE `ProductData` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `productId` int(11) DEFAULT NULL COMMENT '产品ID',
  `companyId` varchar(255) DEFAULT NULL COMMENT '企业ID',
  `downloadCount` bigint(20) NOT NULL DEFAULT 0 COMMENT '下载量',
  `quotationCount` bigint(20) NOT NULL DEFAULT 0 COMMENT '配单量',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleteTime` datetime DEFAULT NULL,
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_productId` (`productId`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;


