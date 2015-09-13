# -*- coding: utf-8 -*-

from fabric.api import run, local, lcd, env, hosts, cd, prompt, put,sudo
from datetime import date,datetime
from fabric.colors import *
from fabric.contrib.files import exists

env.use_ssh_config = True


def deploy():
    """deployします"""

    now = datetime.now().strftime("%Y%m%d_%H%M%S")
    app_name = "ketsuco_{}".format(now)

    # アーカイブ & 配布
    with lcd("../"):
        local("activator dist")
        put("target/universal/ketsuco-1.0-SNAPSHOT.zip", "ketsuco/{}.zip".format(app_name))

    # サーバー作業
    with cd("ketsuco"):

        # 解凍
        run("unzip {}.zip".format(app_name))
        run("mv ketsuco-1.0-SNAPSHOT {}".format(app_name))

        # 旧アプリ停止
        if exists("latest"):
            if exists("latest/RUNNING_PID"):
                run("kill `cat latest/RUNNING_PID`", shell=False)

            #run("ls -la latest | cut -d' ' -f 13 | xargs rm -rf") # シンボリックリンク先削除
            run("rm -f latest")

        run("ln -s {} latest".format(app_name))
        # 最後の起動がなんかうまくいかん。。。動くけど、だめぽ。nohupつけてもだめぽ
        run("latest/bin/ketsuco -Dplay.crypto.secret=abcdefghijk > /dev/null 2>&1 &", pty=False) # TODO crypt


def mig():
    """migrate"""
    pass
