
PROJECT = "ketsuco"

execute "yum groupinstall -y 'Development Tools'"

# fuck selinux
execute "selinux is kuso" do
  user "root"
  command "setenforce 0"
  only_if "getenforce | grep Enforcing"
end

file "/etc/selinux/config" do
  action :edit
  user "root"
  block do |content|
    content.gsub!("SELINUX=enforcing", "SELINUX=disabled")
  end
  only_if "test -e /etc/selinux/config"
end

package "java-1.8.0-openjdk"

directory "create ketsuco dir" do
  action :create
  path "/home/#{node[:user]}/ketsuco"
  mode "777"
  owner "#{node[:user]}"
  group "#{node[:user]}"
end

=begin

package "bzip2-devel"
package "openssl-devel"
package "ncurses-devel"
package "sqlite-devel"
package "readline-devel"
#package "tk-devel"
package "gdbm-devel"
package "db4-devel"
package "libpcap-devel"
package "xz-devel"

package "libjpeg-devel"
package "zlib-devel"
package "git"

package 'http://dev.mysql.com/get/mysql-community-release-el6-5.noarch.rpm' do
  not_if 'rpm -q mysql-community-release-el6-5'
end

package 'mysql-server'
package 'mysql-devel'


service 'mysqld' do
  action [:enable, :start]
end

service 'httpd' do
  user "root"
  action [:enable, :start]
end

directory "create work dir" do
  action :create
  path "/home/#{node[:user]}/work"
  mode "777"
  owner "#{node[:user]}"
  group "#{node[:user]}"
end

  execute "mysql -uroot -e \"CREATE DATABASE if not exists ketsuco CHARACTER SET utf8;\""
execute "mysql -uroot -e \"GRANT ALL ON ketsuco.* to ketsuco@localhost;\""
execute "mysql -uroot -e \"FLUSH PRIVILEGES;\""

=end
