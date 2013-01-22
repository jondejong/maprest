package com.jondejong.maprest

class Person {
    String firstName
    String lastName
    String socialSecurityNumber
    Integer age

    public transformToMap() {
        return [
                firstName: firstName,
                lastName: lastName,
                age: age
        ]
    }
}