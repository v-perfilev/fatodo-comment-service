package contracts.commentcontroller

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name 'add comment'
    description 'should return status 201 and CommentDTO'
    request {
        method POST()
        url($(
                consumer(regex("/api/comment/" + uuid().toString())),
                producer("/api/comment/b73e8418-ff4a-472b-893d-4e248ae93797")
        ))
        headers {
            contentType applicationJson()
            header 'Authorization': $(
                    consumer(containing("Bearer")),
                    producer("Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4ZjlhN2NhZS03M2M4LTRhZDYtYjEzNS01YmQxMDliNTFkMmUiLCJ1c2VybmFtZSI6InRlc3RfdXNlciIsImF1dGhvcml0aWVzIjoiUk9MRV9VU0VSIiwiaWF0IjowLCJleHAiOjMyNTAzNjc2NDAwfQ.Go0MIqfjREMHOLeqoX2Ej3DbeSG7ZxlL4UAvcxqNeO-RgrKUCrgEu77Ty1vgR_upxVGDAWZS-JfuSYPHSRtv-w")
            )
        }
        body($(
                consumer(regex(".+")),
                producer([
                        "text": "test"
                ])
        ))
    }
    response {
        status 201
        headers {
            contentType applicationJson()
        }
        body([
                "id"  : anyUuid(),
                "text": "test"
        ])
    }
}
