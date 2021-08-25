package contracts.commentcontroller

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name 'edit comment'
    description 'should return status 200 and CommentDTO'
    request {
        method PUT()
        url($(
                consumer(regex("/api/comments/" + uuid().toString())),
                producer("/api/comments/6796a82a-93c6-4fdf-bf5d-2da77ce2c338")
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
                        "text": "new_test"
                ])
        ))
    }
    response {
        status 200
        headers {
            contentType applicationJson()
        }
        body([
                "id"  : anyUuid(),
                "text": "new_test"
        ])
    }
}
