# -*- coding: utf-8 -*-

from fabric.api import run, local, lcd, env, hosts, cd, prompt, put, sudo, task
from datetime import date,datetime
from fabric.colors import *
from fabric.contrib.files import exists

import md5

env.use_ssh_config = True

@task
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
                #sudo("kill `cat latest/RUNNING_PID`", shell=False)
                sudo("kill `cat latest/RUNNING_PID`", user="root")

            #run("ls -la latest | cut -d' ' -f 13 | xargs rm -rf") # シンボリックリンク先削除
            run("rm -f latest")

        run("ln -s {} latest".format(app_name))
        # 最後の起動がなんかうまくいかん。。。動くけど、だめぽ。nohupつけてもだめぽ
        secret_key = md5.new(now).hexdigest()
        run("echo {} >> secret_keys".format(secret_key))
        run_cmd = "latest/bin/ketsuco -Dplay.crypto.secret={} -Dplay.evolutions.db.default.autoApply=true > /dev/null 2>&1".format(secret_key)

        #__runbg(run_cmd)
        # sudo("nohup latest/bin/ketsuco -Dplay.crypto.secret={} -Dplay.evolutions.db.default.autoApply=true > /dev/null 2>&1 &".format(secret_key), pty=False)
        #run("nohup latest/bin/ketsuco -Dplay.crypto.secret={} -Dplay.evolutions.db.default.autoApply=true > /dev/null 2>&1 &".format(secret_key), pty=False)
        sudo("latest/bin/ketsuco -Dplay.crypto.secret={} -Dhttp.port=80 -Dplay.evolutions.db.default.autoApply=true > /dev/null 2>&1 &".format(secret_key), pty=False)

@task
def mig():
    """migrate"""
    pass


def __runbg(cmd, sockname="dtach"):
    return run('dtach -n `mktemp -u /tmp/%s.XXXX` %s'  % (sockname, cmd))
