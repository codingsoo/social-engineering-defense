sudo docker build --tag social-engineering-defense .
sudo docker run --name nlp_server --net=host -d social-engineering-defense /bin/sh -c 'cd /social-engineering-defense/scam-question-detector/; ./scripts/start_nlp.sh'
sleep 10
sudo docker run --name nlp_server_demo --net=host -d social-engineering-defense /bin/sh -c 'cd /social-engineering-defense/scam-question-detector/; ./scripts/start_demo.sh'
sleep 30
sudo docker run -it --name demo --net=host social-engineering-defense /bin/bash
cd /social-engineering-defense/check_phishing_with_command

