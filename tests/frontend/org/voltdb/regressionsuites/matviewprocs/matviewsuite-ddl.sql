CREATE TABLE PEOPLE (PARTITION INTEGER NOT NULL, ID INTEGER, AGE INTEGER, SALARY FLOAT, CHILDREN INTEGER, PRIMARY KEY(ID));
CREATE VIEW MATPEOPLE (AGE, NUM, SALARIES, KIDS) AS SELECT AGE, COUNT(*), SUM(SALARY), SUM(CHILDREN) FROM PEOPLE WHERE AGE > 5 GROUP BY AGE;

CREATE TABLE THINGS (ID INTEGER, PRICE INTEGER, PRIMARY KEY(ID));
CREATE VIEW MATTHINGS (PRICE, NUM) AS SELECT PRICE, COUNT(*) FROM THINGS GROUP BY PRICE;
