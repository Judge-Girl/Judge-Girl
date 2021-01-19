./mvnw clean package -DskipTests
cd Judger/ && docker build . -t judger -f Dockerfile.cqi && cd ..
docker-compose -f all-in-one.yaml build
docker-compose -f all-in-one.yaml up