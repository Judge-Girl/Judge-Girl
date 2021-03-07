./mvnw clean package -DskipTests
cd Judger/ && docker build . -t judger -f Dockerfile.cqi && cd ..
docker-compose -f all-in-one.yml build
docker-compose -f all-in-one.yml up