# Thomas' mc_missile_guidance
- `sudo docker run --net host -ti --name mc_missile_guidance_java -v ./:/home/gradle/mc_missile_guidance -p 42069:42069 --entrypoint /bin/bash gradle`

- `./gradlew run`

# Build and publish
- `sudo docker build -t ghcr.io/ufsay/mc_missile_guidance:0.1.0 .`
- `sudo docker push ghcr.io/ufsay/mc_missile_guidance:0.1.0`
