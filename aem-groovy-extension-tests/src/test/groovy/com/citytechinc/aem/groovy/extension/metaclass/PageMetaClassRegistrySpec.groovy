package com.citytechinc.aem.groovy.extension.metaclass

import com.citytechinc.aem.groovy.extension.AbstractGroovyExtensionSpec
import com.day.cq.wcm.api.NameConstants
import spock.lang.Unroll

class PageMetaClassRegistrySpec extends AbstractGroovyExtensionSpec {

    def setup() {
        pageBuilder.content {
            citytechinc("CITYTECH, Inc.") {
                "jcr:content"("sling:resourceType": "foundation/components/page") {
                    mainpar("sling:resourceType": "foundation/components/parsys")
                }
                news()
                company {
                    people()
                    places()
                }
            }
        }
    }

    def "iterator"() {
        setup:
        def page = getPage("/content/citytechinc")

        expect:
        page.iterator().size() == 2
        page*.name == ["news", "company"]
    }

    def "recurse"() {
        setup:
        def page = getPage("/content/citytechinc")
        def names = []

        page.recurse {
            names.add(it.name)
        }

        expect:
        names == ["citytechinc", "news", "company", "people", "places"]
    }

    def "get node"() {
        setup:
        def page = getPage("/content/citytechinc")

        expect:
        page.node.path == "/content/citytechinc/jcr:content"
    }

    def "get node for page with no content"() {
        setup:
        getNode("/content").addNode("empty", NameConstants.NT_PAGE)
        session.save()

        def page = getPage("/content/empty")

        expect:
        !page.node
    }

    @Unroll
    def "get"() {
        setup:
        def page = getPage("/content/citytechinc")

        expect:
        page.get(propertyName) == propertyValue

        where:
        propertyName          | propertyValue
        "sling:resourceType"  | "foundation/components/page"
        "nonExistentProperty" | null
    }

    def "set"() {
        setup:
        def page = getPage("/content/citytechinc")

        when:
        page.set(propertyName, propertyValue)

        then:
        page.get(propertyName) == propertyValue

        where:
        propertyName              | propertyValue
        "sling:resourceSuperType" | "foundation/components/parbase"
        "nonExistentProperty"     | null
    }

    def "set for page with no content"() {
        setup:
        getNode("/content").addNode("empty", NameConstants.NT_PAGE)
        session.save()

        def page = getPage("/content/empty")

        when:
        page.set("foo", "bar")

        then:
        notThrown(NullPointerException)
    }
}
