#/bin/sh

# vagrant ssh-config >> ~/.ssh/config

select TARGET in vag_deploy vag_migration prod_deploy prod_migration
do
    if [ "${TARGET}" = "vag_deploy" ]; then
		fab -H vagrant deploy
		break;
	fi
	if [ "${TARGET}" = "vag_migration" ]; then
		fab -H vagrant mig
		break;
	fi
	if [ "${TARGET}" = "ec2_deploy" ]; then
		fab -H ec2 deploy
		break;
	fi
	if [ "${TARGET}" = "vag_migration" ]; then
		fab -H ec2 mig
		break;
	fi
    if [ "${TARGET}" = "prod_deploy" ]; then
		fab -H sandbox deploy
		break;
	fi
	if [ "${TARGET}" = "prod_migration" ]; then
		fab -H sandbox mig
		break;
	fi
	echo "hey!"
done
