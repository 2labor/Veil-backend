FROM rabbitmq:3.12-management

RUN apt-get update && apt-get install -y curl \
    && curl -L https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.12.0/rabbitmq_delayed_message_exchange-3.12.0.ez \
       -o $RABBITMQ_HOME/plugins/rabbitmq_delayed_message_exchange-3.12.0.ez \
    && rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange \
    && rabbitmq-plugins enable --offline rabbitmq_stomp \
    && apt-get remove -y curl \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*