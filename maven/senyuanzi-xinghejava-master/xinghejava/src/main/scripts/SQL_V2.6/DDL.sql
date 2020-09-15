alter table `tbaccount`
    ADD COLUMN `IsPaid` int(11) NULL COMMENT '是否付费' AFTER `provinceId`;
alter table `tbproduct`
    ADD COLUMN `IsInExclusive` int(11) NOT NULL DEFAULT 0  AFTER `state`;
alter table `tbfriend`
    ADD COLUMN `isAuth` int(11) NOT NULL DEFAULT 0 ;
alter table `tbaccount`
    ADD COLUMN `starNumber` int(11) NOT NULL DEFAULT 0 ;

CREATE TABLE `ProductShare` (
    `Id` int(11) NOT NULL AUTO_INCREMENT,
    `AccountId` varchar(50) NOT NULL,
    `ProductId` int(11) NOT NULL,
    `ToAccountId` varchar(50) NOT NULL,
    `SalePrice` decimal(11,2) DEFAULT NULL,
    `IsReceived` int(11) DEFAULT '0',
    `UpdateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `CreateTime` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`Id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE `GoEasyMessage` (
     `Id` int(11) NOT NULL AUTO_INCREMENT,
     `Channel` varchar(50) DEFAULT NULL,
     `Content` text,
     `IsPush` int(11) DEFAULT NULL COMMENT '是否推送',
     `IsRead` int(11) DEFAULT NULL COMMENT '是否已读',
     `CreateTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `UpdateTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     PRIMARY KEY (`Id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;