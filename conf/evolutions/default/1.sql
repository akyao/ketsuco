# --- !Ups
CREATE TABLE cron(
   id bigint(20) NOT NULL AUTO_INCREMENT,
   body text,
   hash varchar(64),
   created_at datetime,
   updated_at datetime,
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
   created_at datetime,
   updated_at datetime,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;

CREATE INDEX idx1 ON cron_line (cron_id ASC, line ASC);
ALTER TABLE cron_line ADD FOREIGN KEY (cron_id) REFERENCES cron (id)
	ON UPDATE RESTRICT ON DELETE RESTRICT;


INSERT INTO `cron` (`id`,`body`,`hash`,`created_at`,`updated_at`)
VALUES (1,'MAILTO=tests@kestuco.com\r\n\r\n# 一日一回のログ\r\n10      3      *       *       *       /home/hoge/hoge1.sh\r\n10      4      *       *       *       /home/hoge/hoge2.sh\r\n10      5      *       *       *       /home/hoge/hoge3.sh\r\n\r\n## 一日一回以外のログ\r\n*/10      *      *       *       *       /home/hoge/hoge11.sh\r\n30-59/5      0-12      *       *       *       /home/hoge/hoge12.sh\r\n15,35,55      */2      *       *       *       /home/hoge/hoge13.sh\r\n\r\n# 分と時以外の設定は無視\r\n30      0      *       *       1       /home/hoge/hoge21.sh\r\n30      1      1,10,20,30       *       *       /home/hoge/hoge22.sh\r\n30      2      1       3,9       *       /home/hoge/hoge23.sh','sample',now(),now());


INSERT INTO `cron_line` (`cron_id`,`line`,`body`,`command`,`month`,`day`,`week`,`hour`,`minute`,`created_at`,`updated_at`)
VALUES
 (1,3,'10 3 * * * /home/hoge/hoge1.sh','/home/hoge/hoge1.sh','*','*','*','3','10',now(), now()),
 (1,4,'10 4 * * * /home/hoge/hoge2.sh','/home/hoge/hoge2.sh','*','*','*','4','10',now(), now()),
 (1,5,'10 5 * * * /home/hoge/hoge3.sh','/home/hoge/hoge3.sh','*','*','*','5','10',now(), now()),
 (1,8,'*/10 * * * * /home/hoge/hoge11.sh','/home/hoge/hoge11.sh','*','*','*','*','*/10',now(), now()),
 (1,9,'30-59/5 0-12 * * * /home/hoge/hoge12.sh','/home/hoge/hoge12.sh','*','*','*','0-12','30-59/5',now(), now()),
 (1,10,'15,35,55 */2 * * * /home/hoge/hoge13.sh','/home/hoge/hoge13.sh','*','*','*','*/2','15,35,55',now(), now()),
 (1,13,'30 0 * * 1 /home/hoge/hoge21.sh','/home/hoge/hoge21.sh','*','*','1','0','30',now(), now()),
 (1,14,'30 1 1,10,20,30 * * /home/hoge/hoge22.sh','/home/hoge/hoge22.sh','*','1,10,20,30','*','1','30',now(), now()),
 (1,15,'30 2 1 3,9 * /home/hoge/hoge23.sh','/home/hoge/hoge23.sh','3,9','1','*','2','30',now(), now());

# --- !Downs
DROP TABLE cron_line;
DROP TABLE cron;