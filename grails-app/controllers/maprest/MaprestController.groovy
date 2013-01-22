package maprest

import com.jondejong.maprest.Address
import com.jondejong.maprest.Family
import com.jondejong.maprest.Person

class MaprestController {

    def index() {
        def object = getFamily()

        renderMaprest(object, "testElement", xmlFormat)
    }

    protected getFamily() {

        def children = [
                new Person(firstName: 'Jamie', lastName: 'Johnson', socialSecurityNumber: '111-22-3333', age:  14),
                new Person(firstName: 'Jordan', lastName: 'Johnson', socialSecurityNumber: '222-33-4444', age:  9)
        ]

        def parents = [
                new Person(firstName: 'John', lastName: 'Johnson', socialSecurityNumber: '333-44-555', age: 42),
                new Person(firstName: 'Jasmine', lastName: 'Johnson', socialSecurityNumber: '444-55-666', age: 41)
        ]

        def address = new Address(line1:'123 Awesomesauce Drive', city: "Minneapolis", state: "Minnesota", zipCode: "55401", type: 'home')

        new Family(parents: parents, children: children, address: address)
    }

}
