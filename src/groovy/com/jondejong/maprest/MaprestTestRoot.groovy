package com.jondejong.maprest
/**
 * Created with IntelliJ IDEA.
 * User: jondejong
 * Date: 1/20/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
class MaprestTestRoot {

    String firstProperty
    String secoondProoperty

    def transformToMap() {
        [firstProperty: firstProperty, secoondProoperty: secoondProoperty]
    }
}
