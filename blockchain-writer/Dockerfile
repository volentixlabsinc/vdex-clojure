FROM clojure 

ENV KAFKA_HOST kafka:9092
ADD ./ /code
WORKDIR /code
EXPOSE 5000

CMD ["lein", "run"]
