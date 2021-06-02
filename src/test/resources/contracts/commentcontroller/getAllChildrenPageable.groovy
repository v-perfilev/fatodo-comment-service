package contracts.commentcontroller

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name 'get pageable children by thread id'
    description 'should return status 200 and list of CommentDTO'
    request {
        method GET()
        url($(
                consumer(regex("/api/comments/" + uuid().toString() + "/children.*")),
                producer("/api/comments/6796a82a-93c6-4fdf-bf5d-2da77ce2c338/children")
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
                [
                        "id"  : "6520f3e6-0a7f-4c32-b6f8-ba5ae3ed0bd1",
                        "text": anyNonEmptyString()
                ]
        ])
    }
}
