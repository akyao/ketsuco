#/bin/sh

select TARGET in vagrant ec2 prod
do
    if [ "${TARGET}" = "vagrant" ]; then
		itamae ssh --vagrant -u vagrant -j itamae_node_vagrant.json itamae.rb
		break;
	fi
	if [ "${TARGET}" = "ec2" ]; then
		itamae ssh -h ec2 -u ec2-user -j itamae_node_ec2.json itamae.rb
		break;
	fi
    if [ "${TARGET}" = "prod" ]; then
		itamae ssh -h sandbox -u akyao -j itamae_node_prod.json itamae.rb
		break;
	fi
	echo "hey!"
done
