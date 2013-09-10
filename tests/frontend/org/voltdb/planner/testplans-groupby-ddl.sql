CREATE TABLE R1 (
	PKEY INTEGER NOT NULL,
	A1 INTEGER NOT NULL,
	B1 INTEGER NOT NULL,
	C1 INTEGER NOT NULL,
	D1 INTEGER NOT NULL,		
	PRIMARY KEY (PKEY)
);

CREATE TABLE P1 (
	PKEY INTEGER NOT NULL,
	A1 INTEGER NOT NULL,
	B1 INTEGER NOT NULL,
	C1 INTEGER NOT NULL,
	D1 INTEGER NOT NULL,		
	PRIMARY KEY (PKEY)
);

PARTITION TABLE P1 ON COLUMN PKEY;

CREATE TABLE T1 (
	PKEY INTEGER NOT NULL,
	A1 INTEGER NOT NULL,	
	PRIMARY KEY (PKEY)
);

PARTITION TABLE T1 ON COLUMN PKEY;

CREATE TABLE D1 (
	D1_PKEY INTEGER NOT NULL,
	D1_NAME VARCHAR(10) NOT NULL,
	PRIMARY KEY (D1_PKEY)
);

CREATE TABLE D2 (
	D2_PKEY INTEGER NOT NULL,
	D2_NAME VARCHAR(10) NOT NULL,
	PRIMARY KEY (D2_PKEY)
);

CREATE TABLE D3 (
	D3_PKEY INTEGER NOT NULL,
	D3_NAME VARCHAR(10) NOT NULL,
	PRIMARY KEY (D3_PKEY)
);

CREATE TABLE F (
	F_PKEY INTEGER NOT NULL,
	F_D1   INTEGER NOT NULL,
	F_D2   INTEGER NOT NULL,
	F_D3   INTEGER NOT NULL,
	F_VAL1 INTEGER NOT NULL,
	F_VAL2 INTEGER NOT NULL,
	F_VAL3 INTEGER NOT NULL,
	PRIMARY KEY (F_PKEY)
);

PARTITION TABLE F ON COLUMN F_PKEY;

CREATE INDEX COL_F_TREE1 ON F (F_D1);
CREATE INDEX COL_F_TREE2 ON F (F_VAL1, F_VAL2);
CREATE UNIQUE INDEX EXPR_F_TREE1 ON F (F_D1 + F_D2);
CREATE INDEX EPXR_F_TREE2 ON F (ABS(F_D1), F_D2 - F_D3);

CREATE TABLE RF (
    F_PKEY INTEGER NOT NULL,
    F_D1   INTEGER NOT NULL,
    F_D2   INTEGER NOT NULL,
    F_D3   INTEGER NOT NULL,
    F_VAL1 INTEGER NOT NULL,
    F_VAL2 INTEGER NOT NULL,
    F_VAL3 INTEGER NOT NULL,
    PRIMARY KEY (F_PKEY)
);

CREATE INDEX COL_RF_TREE1 ON RF (F_D1);
CREATE INDEX COL_RF_TREE2 ON RF (F_VAL1, F_VAL2);
CREATE UNIQUE INDEX EXPR_RF_TREE1 ON RF (F_D1 + F_D2);
CREATE INDEX EPXR_RF_TREE2 ON RF (ABS(F_D1), F_D2 - F_D3);

CREATE TABLE B (
	B_PKEY INTEGER NOT NULL,
	B_VAL1 VARBINARY(6) NOT NULL,
	PRIMARY KEY (B_PKEY)
);

CREATE VIEW V (V_D1_PKEY, V_D2_PKEY, V_D3_PKEY, V_F_PKEY, CNT, SUM_V1, SUM_V2, SUM_V3)
    AS SELECT F_D1, F_D2, F_D3, F_PKEY, COUNT(*), SUM(F_VAL1), SUM(F_VAL2), SUM(F_VAL3)
    FROM F  GROUP BY F_D1, F_D2, F_D3, F_PKEY;

CREATE TABLE T2 (
	PKEY INTEGER NOT NULL,
	I INTEGER NOT NULL,
	PRIMARY KEY (PKEY)
);

PARTITION TABLE T2 ON COLUMN PKEY;
