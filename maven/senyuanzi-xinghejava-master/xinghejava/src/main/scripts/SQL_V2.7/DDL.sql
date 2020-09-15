
CREATE TABLE `Cooperation` (
    `Id` int(11) NOT NULL AUTO_INCREMENT,
    `AccountId` varchar(50) NOT NULL,
    `ProjectId` varchar(50) NOT NULL,
    `CooperativeAccountId` varchar(50) NOT NULL,
    `IsOK` int(11) DEFAULT '0',
    `UpdateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `CreateTime` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`Id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
