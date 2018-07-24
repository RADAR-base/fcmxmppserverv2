
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:8-alpine AS builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_OPTS -Dorg.gradle.daemon=false

COPY ./gradle /code/gradle
COPY ./gradlew /code/
RUN ./gradlew --version

COPY ./build.gradle ./settings.gradle /code/

RUN ./gradlew downloadDependencies copyDependencies startScripts

COPY ./src /code/src

RUN ./gradlew jar

FROM openjdk:8-jre-alpine

MAINTAINER Yatharth Ranjan <yatharth.ranjan@kcl.ac.uk>

LABEL description="RADAR-base XMPP server for FCM"

COPY --from=builder /code/build/third-party/* /usr/lib/
COPY --from=builder /code/build/scripts/* /usr/bin/
COPY --from=builder /code/build/libs/* /usr/lib/

ENTRYPOINT ["radar-xmppserver"]