DROP TABLE T_FOOS;
DROP TABLE T_WRITE_FOOS;

CREATE TABLE T_FOOS (
	ID BIGINT NOT NULL,
	NAME VARCHAR(45),
	VALUE BIGINT
);

ALTER TABLE T_FOOS ADD PRIMARY KEY (ID);

INSERT INTO t_foos (id, name, value) VALUES (1, 'bar2', 2);
INSERT INTO t_foos (id, name, value) VALUES (2, 'bar4', 4);
INSERT INTO t_foos (id, name, value) VALUES (3, 'bar1', 1);
INSERT INTO t_foos (id, name, value) VALUES (4, 'bar5', 5);
INSERT INTO t_foos (id, name, value) VALUES (5, 'bar3', 3);

CREATE TABLE T_WRITE_FOOS (
	ID BIGINT NOT NULL,
	NAME VARCHAR(45),
	VALUE BIGINT
);

ALTER TABLE T_WRITE_FOOS ADD PRIMARY KEY (ID);
