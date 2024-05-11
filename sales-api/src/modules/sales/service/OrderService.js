import OrderRepository from "../repository/OrderRepository.js";
import { sendMessageToProductStockUpdateQueue } from "../../product/rabbitmq/productStockUpdateSender.js";
import { PENDING } from "../status/OrderStatus.js";
import OrderException from "../exception/OrderException.js";
import { BAD_REQUEST, INTERNAL_SERVER_ERROR, SUCCESS } from "../../../config/constants/httpStatus.js";
import ProductClient from "../../product/client/ProductClient.js";

class OrderService {
    async createOrder(req) {
        try {
            let orderData = req.body;
            const { transactionid, serviceid } = req.headers;
            console.info(`Request to POST new order with data ${JSON.stringify(orderData)} | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            this.validateOrderData(orderData);
            const { authUser } = req;
            const { authorization } = req.headers;
            let order = this.createInitialOrderData(orderData, authUser, transactionid, serviceid);
            await this.validateProductStock(order, authorization, transactionid);
            let createdOrder = await OrderRepository.save(order);
            this.sendMessage(createdOrder, transactionid);
            let response = {
                status: SUCCESS, createdOrder
            };
            console.info(`Response to POST new order with data ${JSON.stringify(response)} | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            return response;
        } catch (error) {
            return {
                status: error.status ? error.status : INTERNAL_SERVER_ERROR, message: error.message
            };
        }
    }
    createInitialOrderData(orderData, authUser, transactionid, serviceid) {
        return {
            status: PENDING,
            user: authUser,
            createdAt: new Date(),
            updatedAt: new Date(),
            transactionid,
            serviceid,
            products: orderData.products
        };
    }
    async updateOrder(orderMessage) {
        try {
            const order = JSON.parse(orderMessage);
            if (order.salesId && order.status) {
                let existingOrder = await OrderRepository.findById(order.salesId);
                if (existingOrder && order.status !== existingOrder.status) {
                    existingOrder.status = order.status;
                    existingOrder.updatedAt = new Date();
                    await OrderRepository.save(existingOrder);
                }
            } else {
                console.warn(`The order message was not complete. transactionId: ${orderMessage.transactionid}`);
            }
        } catch (error) {
            console.error("Could not parse order message from queue.");
            console.error(error.message);
        }
    }
    validateOrderData(data) {
        if (!data || !data.products) {
            throw new OrderException(BAD_REQUEST, "The products must be informed.");
        }
    }
    async validateProductStock(order, token, transactionid) {
        let stockIsOK = await ProductClient.checkProductStock(order, token, transactionid);
        if (!stockIsOK) {
            throw new OrderException(BAD_REQUEST, "The stock is out for the products.");
        }
    }
    sendMessage(createOrder, transactionid) {
        const message = { salesId: createOrder.id, products: createOrder.products, transactionid };
        sendMessageToProductStockUpdateQueue(message);
    }
    async findById(req) {
        try {
            const { id } = req.params;
            const { transactionid, serviceid } = req.headers;
            console.info(`Request to GET order by ID ${id} | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            this.validateInformedId(id);
            const existingOrder = await OrderRepository.findById(id);
            if (!existingOrder) { throw new OrderException(BAD_REQUEST, "The order was not found."); }
            let response = { status: SUCCESS, existingOrder };
            console.info(`Response to GET order by ID ${id}: ${JSON.stringify(response)} | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            return response;
        } catch (error) {
            return { status: error.status ? error.status : INTERNAL_SERVER_ERROR, message: error.message };
        }
    }
    async findAll(req) {
        try {
            const { transactionid, serviceid } = req.headers;
            console.info(`Request to GET all orders | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            const orders = await OrderRepository.findAll();
            if (!orders) { throw new OrderException(BAD_REQUEST, "No orders were found."); }
            let response = { status: SUCCESS, orders };
            console.info(`Response to GET all orders: ${JSON.stringify(response)} | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            return response;
        } catch (error) {
            return { status: error.status ? error.status : INTERNAL_SERVER_ERROR, message: error.message };
        }
    }
    async findByProductId(req) {
        try {
            const { productId } = req.params;
            const { transactionid, serviceid } = req.headers;
            console.info(`Request to GET orders by productId ${productId} | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            this.validateInformedProductId(productId);
            const orders = await OrderRepository.findByProductId(productId);
            if (!orders) { throw new OrderException(BAD_REQUEST, "No orders were found."); }
            let response = { status: SUCCESS, salesIds: orders.map((order) => { return order.id; }) };
            console.info(`Response to GET orders by productId: ${productId}: ${JSON.stringify(response)} | [transactionId: ${transactionid} | serviceId: ${serviceid}]`);
            return response;
        } catch (error) {
            return { status: error.status ? error.status : INTERNAL_SERVER_ERROR, message: error.message };
        }
    }
    validateInformedId(id) {
        if (!id) { throw new OrderException(BAD_REQUEST, "The order ID must be informed."); }
    }
    validateInformedProductId(id) {
        if (!id) { throw new OrderException(BAD_REQUEST, "The order's productId must be informed."); }
    }
}

export default new OrderService();