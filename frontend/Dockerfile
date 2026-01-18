FROM nginx:1.27-alpine

# Удаляем дефолтную конфигурацию
RUN rm /etc/nginx/conf.d/default.conf

# Копируем наш конфиг nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Копируем собранный фронт
COPY dist /usr/share/nginx/html

EXPOSE 80

