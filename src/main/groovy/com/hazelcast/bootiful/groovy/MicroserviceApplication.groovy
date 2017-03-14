package com.hazelcast.bootiful.groovy

import com.hazelcast.config.Config
import com.hazelcast.config.EntryListenerConfig
import com.hazelcast.config.XmlConfigBuilder
import com.hazelcast.core.EntryEvent
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import com.hazelcast.map.listener.EntryAddedListener
import com.hazelcast.map.listener.EntryRemovedListener
import com.hazelcast.map.listener.EntryUpdatedListener
import com.hazelcast.map.listener.MapListener
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.*

@SpringBootApplication
@EnableCaching
@Slf4j
@CompileStatic
class MicroserviceApplication {

    @Bean
    Config getConfig(MapListener listener) {
        EntryListenerConfig listenerConfig = new EntryListenerConfig()
        listenerConfig.setIncludeValue(true).setImplementation(listener)
        def config = new XmlConfigBuilder()
                .build()
        config.getMapConfig("my-cache").addEntryListenerConfig listenerConfig
        config
    }

    @Bean
    IMap<String, String> getCacheMap(HazelcastInstance hazelcast) { hazelcast.getMap "my-cache" }

    /**
     * | HTTP     | CRUD      | Hazelcast   | httpie
     * ----------------------------------------------
     * | POST     | create    | set         | `http POST :8080/caching/1 value==test`
     * | GET      | read      | get         | `http :8080/caching/1`
     * | PUT      | update    | put         | `http PUT :8080/caching/1 value==test2`
     * | PATCH    | update    | replace     | `http PATCH :8080/caching/1 oldValue==test2 newValue=="hey hey"`
     * | DELETE   | delete    | remove      | `http DELETE :8080/caching/1`
     * ----------------------------------
     */
    @RestController
    @RequestMapping("caching")
    class MyController {

        @Autowired
        private IMap<String, String> cacheMap

        @RequestMapping(method = POST, path = "{key}", params = ["value"])
        def post(@PathVariable String key, @RequestParam String value) { cacheMap.set key, value }

        @RequestMapping(
                method = GET,
                path = "/{key}")
        def get(@PathVariable("key") String key) { cacheMap[key] }

        @RequestMapping(method = PUT, path = "{key}", params = ["value"])
        def put(@PathVariable String key, @RequestParam String value) { cacheMap[key] = value }

        @RequestMapping(method = PATCH, path = "/{key}", params = ["oldValue", "newValue"])
        def patch(@PathVariable("key") String key,
                  @RequestParam("oldValue") String value,
                  @RequestParam("newValue") String newValue) {
            cacheMap.replace key, value, newValue
        }

        @RequestMapping(method = DELETE, path = "/{key}")
        def delete(@PathVariable String key) { cacheMap.remove key }

    }

    @Component
    class MyListener implements
            EntryAddedListener<String, String>,
            EntryUpdatedListener<String, String>,
            EntryRemovedListener<String, String> {

        @Override
        void entryAdded(EntryEvent<String, String> event) {
            log.info "Entry added [{} : {}]", event.getKey(), event.getValue()
        }

        @Override
        void entryUpdated(EntryEvent<String, String> event) {
            log.info "Entry updated [{} : {}]. Old value {}", event.getKey(), event.getValue(), event.getOldValue()
        }

        @Override
        void entryRemoved(EntryEvent<String, String> event) {
            log.info "Entry removed [{} : {}]", event.getKey(), event.getOldValue()
        }
    }

    static void main(String... args) {
        SpringApplication.run MicroserviceApplication.class, args
    }
}