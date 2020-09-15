
-- originProductId修复
update tbproduct
set OriginProductId=ProductId
WHERE AccountId=OriginAccountId;

-- OriginAccountId数据修复
UPDATE tbproduct T
SET T.OriginProductId=
(
SELECT P.ProductId
FROM  ( select * FROM tbproduct WHERE OriginAccountId=AccountId ) P
WHERE P.ProductName=T.ProductName
AND P.AccountId=T.OriginAccountId
AND P.OriginAccountId=T.OriginAccountId
LIMIT 1
)
WHERE T.AccountId!=T.OriginAccountId;

-- init ProductData表
INSERT INTO ProductData ( productId, companyId, downloadCount, quotationCount )
SELECT R.productId, R.companyId,ifnull(R.downloadCount,0) downloadCount,IFNULL(R.quotationCount,0) quotationCount
FROM
(
	SELECT
		U.OriginProductId productId,
		U.OriginAccountId companyId,
		SUM( U.downloadCount ) downloadCount,
		SUM( U.quotationCount ) quotationCount
	FROM
		(
			(
			SELECT
				P.OriginProductId,
				P.OriginAccountId,
				COUNT( P.ProductId ) downloadCount,
				PD.quotationCount
			FROM
				tbproduct P
				LEFT JOIN ProductData PD ON P.OriginProductId = PD.productId
			WHERE
				P.AccountId != P.OriginAccountId
			GROUP BY
				P.OriginProductId
			)
			UNION
			(
			SELECT
				P2.OriginProductId,
				P2.OriginAccountId,
				PD2.downloadCount,
				COUNT( DISTINCT Q.BillId ) quotationCount
			FROM
				tbproduct P2
				LEFT JOIN tbquotationbillproduct Q ON P2.ProductId = Q.ProductId
				LEFT JOIN ProductData PD2 ON P2.OriginProductId = PD2.productId
			WHERE (Q.ProductId REGEXP "[^0-9.]")=0
			GROUP BY
				P2.OriginProductId
			) -- ORDER BY CAST(OriginProductId as UNSIGNED) asc
		) U
	WHERE U.OriginAccountId is not NULL
	-- AND (U.OriginProductId REGEXP "[^0-9.]")=0
	GROUP BY
		U.OriginProductId
	 ORDER BY CAST( U.OriginProductId AS UNSIGNED ) ASC
) R;

-- init WholesalerData表
INSERT INTO WholesalerData (  companyId, productDownloadCount, productQuotationCount )
SELECT  A.AccountId,IFNULL(SUM(PD.downloadCount),0) productDownloadCount,IFNULL(SUM(PD.quotationCount),0) productQuotationCount
FROM tbaccount A
LEFT JOIN ProductData PD ON A.AccountId=PD.companyId
GROUP BY A.AccountId;

-- BATCH DEPARTMENT
CALL BacthDepartment();

-- 项目阶段
CALL BatchInsertProjectStage();

UPDATE tbproduct SET SortNumber = ProductId ;