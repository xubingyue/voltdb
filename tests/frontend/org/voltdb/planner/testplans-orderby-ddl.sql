CREATE TABLE T (
	T_PKEY INTEGER NOT NULL,
	T_D1   INTEGER NOT NULL,
	T_D2   INTEGER NOT NULL,
	CONSTRAINT T_TREE PRIMARY KEY (T_PKEY,T_D1)
);
