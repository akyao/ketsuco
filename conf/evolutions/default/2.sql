# --- !Ups

CREATE TABLE hbf_user(
   id bigint(20) NOT NULL AUTO_INCREMENT,
   user_name text,
   created_at datetime,
   updated_at datetime,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE hbf_site(
   id bigint(20) NOT NULL AUTO_INCREMENT,
   url bigint(20) NOT NULL,
   created_at datetime,
   updated_at datetime,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE hbf_site_page(
   id bigint(20) NOT NULL AUTO_INCREMENT,
   hbf_site_id bigint(20) NOT NULL,
   url bigint(20) NOT NULL,
   entry_at datetime,
   created_at datetime,
   updated_at datetime,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE hbf_bookmark(
   id bigint(20) NOT NULL AUTO_INCREMENT,
   hbf_site_page_id bigint(20) NOT NULL,
   hbf_user_id bigint(20) NOT NULL,
   created_at datetime,
   updated_at datetime,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;


ALTER TABLE hbf_site_entry ADD FOREIGN KEY (hbf_site_id) REFERENCES hbf_site (id)
	ON UPDATE RESTRICT ON DELETE RESTRICT;

CREATE INDEX idx1 ON hbf_bookmark (hbf_site_entry_id ASC, hbf_user_id ASC);
ALTER TABLE hbf_bookmark ADD FOREIGN KEY (hbf_site_page_id) REFERENCES hbf_site_page (id)
	ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE hbf_bookmark ADD FOREIGN KEY (hbf_user_id) REFERENCES hbf_user (id)
    	ON UPDATE RESTRICT ON DELETE RESTRICT;


# --- !Downs
DROP TABLE hbf_bookmark;
DROP TABLE hbf_site_page;
DROP TABLE hbf_site;
DROP TABLE hbf_user;
