# --- !Ups
CREATE TABLE cron(
   id bigint(20) NOT NULL AUTO_INCREMENT,
   body text,
   hash varchar(64),
   created_at date,
   updated_at date,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE cron_line(
   id bigint(20) NOT NULL AUTO_INCREMENT,
   cron_id bigint(20) NOT NULL,
   line int,
   body varchar(1024),
   command varchar(1024),
   month varchar(1024),
   day varchar(1024),
   week varchar(1024),
   hour varchar(1024),
   minute varchar(1024),
   created_at date,
   updated_at date,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;

CREATE INDEX idx1 ON cron_line (cron_id ASC, line ASC);
ALTER TABLE cron_line ADD FOREIGN KEY (cron_id) REFERENCES cron (id)
	ON UPDATE RESTRICT ON DELETE RESTRICT;

# --- !Downs
DROP TABLE cron_line;
DROP TABLE cron;