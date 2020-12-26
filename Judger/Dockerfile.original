### Get Linux
FROM nvidia/cuda:10.0-base
RUN apt-get update && apt-get install -y gcc clinfo ocl-icd-libopencl1 opencl-headers ocl-icd-opencl-dev
RUN apt-get update && apt-get install -y cgroup-bin cgroup-lite libcgroup1
RUN apt-get update && apt-get install -y libseccomp-dev libseccomp2 seccomp
RUN mkdir -p /etc/OpenCL/vendors && echo "libnvidia-opencl.so.1" >/etc/OpenCL/vendors/nvidia.icd

# Prepare installation of Oracle Java 8
ENV JAVA_VER 8
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# This is in accordance to : https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-get-on-ubuntu-16-04
RUN apt-get update && \
	apt-get install -y openjdk-8-jdk && \
	apt-get install -y ant && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

# Fix certificate issues, found as of
# https://bugs.launchpad.net/ubuntu/+source/ca-certificates-java/+bug/983302
RUN apt-get update && \
	apt-get install -y ca-certificates-java && \
	apt-get clean && \
	update-ca-certificates -f && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

# Setup JAVA_HOME, this is useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# Install wget
RUN apt-get update && apt-get install -y wget

# Install maven 3.3.9
RUN wget --no-verbose -O /tmp/apache-maven-3.3.9-bin.tar.gz http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz && \
    tar xzf /tmp/apache-maven-3.3.9-bin.tar.gz -C /opt/ && \
    ln -s /opt/apache-maven-3.3.9 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/local/bin  && \
    rm -f /tmp/apache-maven-3.3.9-bin.tar.gz

ENV MAVEN_HOME /opt/maven

# Prepare judger.jar
COPY target/judger-0.0.1-SNAPSHOT-jar-with-dependencies.jar /judger.jar

# Compile Profiler and copy it into judger home
COPY CC-Profiler-Sandbox/ /CC-Profiler-Sandbox/
WORKDIR /CC-Profiler-Sandbox
RUN gcc -std=c99 -o profiler main.c sandbox.c profiler.c logger.c killer.c rules/general.c rules/c_cpp.c rules/c_cpp_file_io.c -lpthread -lseccomp
RUN mkdir -p /judger/run && cp profiler /judger/run/profiler

WORKDIR /

CMD java -jar judger.jar



