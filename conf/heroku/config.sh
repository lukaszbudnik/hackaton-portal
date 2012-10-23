# Twilio variables have to be set manually as there is no Twilio add-on in Heroku
# it is assumed that those settings are set locally ${TWILIO_*}
heroku config:set PLAY_EHCACHE_PLUGIN=disabled PLAY_MEMCACHED_PLUGIN=enabled TWILIO_APPLICATION_SID=${TWILIO_APPLICATION_SID} TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}