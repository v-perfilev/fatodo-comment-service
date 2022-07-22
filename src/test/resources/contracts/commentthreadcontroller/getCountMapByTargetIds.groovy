package contracts.commentthreadcontroller

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name 'get count map by targetIds'
    description 'should return status 200 and map of UUID and Long'
    request {
        method GET()
        url($(
                consumer(regex("/api/thread/count\\?ids=.*")),
                producer("/api/thread/count?ids=b73e8418-ff4a-472b-893d-4e248ae93797")
        ))
        headers {
            header 'Authorization': $(
                    consumer(containing("Bearer")),
                    producer("Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4ZjlhN2NhZS03M2M4LTRhZDYtYjEzNS01YmQxMDliNTFkMmUiLCJ1c2VybmFtZSI6InRlc3RfdXNlciIsImF1dGhvcml0aWVzIjoiUk9MRV9VU0VSIiwiaWF0IjowLCJleHAiOjMyNTAzNjc2NDAwfQ.Go0MIqfjREMHOLeqoX2Ej3DbeSG7ZxlL4UAvcxqNeO-RgrKUCrgEu77Ty1vgR_upxVGDAWZS-JfuSYPHSRtv-w")
            )
        }
    }
    response {
        status 200
        headers {
            contentType applicationJson()
        }
        body([
                "b73e8418-ff4a-472b-893d-4e248ae93797": "2"
        ])
    }
}
