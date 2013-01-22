package com.jondejong.maprest

/**
 * Created By: Jon DeJong
 * Date: 1/11/13
 * Time: 2:13 PM
 */
class Address {

    String line1
    String line2
    String city
    String state
    String zipCode
    String type

    public transformToMap() {
        return [
                line1: line1,
                line2: line2,
                city: city,
                state: state,
                zipCode: zipCode,
                "@type": type
        ]
    }
}
